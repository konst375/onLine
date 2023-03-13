package com.chirko.onLine.user.service;

import com.chirko.onLine.common.dto.AuthenticationResponse;
import com.chirko.onLine.img.service.ImgService;
import com.chirko.onLine.secure.token.accessToken.service.AccessTokenService;
import com.chirko.onLine.secure.token.commonToken.exception.CommonTokenExpiredException;
import com.chirko.onLine.secure.token.commonToken.exception.CommonTokenForSuchUserNotFoundException;
import com.chirko.onLine.secure.token.commonToken.exception.InvalidCommonTokenException;
import com.chirko.onLine.secure.token.commonToken.service.CommonTokenService;
import com.chirko.onLine.user.dto.ResetUserPasswordDto;
import com.chirko.onLine.user.entity.User;
import com.chirko.onLine.user.event.OnPasswordResetRequestEvent;
import com.chirko.onLine.user.event.OnSuccessfulPasswordResetEvent;
import com.chirko.onLine.user.exception.UserEmailNotFoundException;
import com.chirko.onLine.user.repo.UserRepo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AccessTokenService accessTokenService;
    private final CommonTokenService commonTokenService;
    private final ImgService imgService;

    public void resetPassword(String email) throws UserEmailNotFoundException {
        User user = userRepo.findByEmail(email).orElseThrow(UserEmailNotFoundException::new);

        String token = commonTokenService.createSaveAndGetCommonTokenForUser(user);

        applicationEventPublisher.publishEvent(new OnPasswordResetRequestEvent(user, token));
    }

    @Transactional
    public AuthenticationResponse saveResetPassword(
            @Valid ResetUserPasswordDto resetUserPasswordDto
    ) throws UserEmailNotFoundException, CommonTokenExpiredException, InvalidCommonTokenException, CommonTokenForSuchUserNotFoundException {

        String token = resetUserPasswordDto.getToken();
        commonTokenService.validateToken(token);

        User user = commonTokenService.extractUser(token);

        user.setPassword(passwordEncoder.encode(resetUserPasswordDto.getPassword()));

        applicationEventPublisher.publishEvent(new OnSuccessfulPasswordResetEvent(user));

        commonTokenService.deleteCommonTokenForUser(user);

        return AuthenticationResponse.builder()
                .jwtToken(accessTokenService.generateAccessToken(user))
                .build();
    }

    public boolean isOldPasswordValid(String email, String oldPassword) throws UserEmailNotFoundException {
        return findUserByEmail(email).getPassword().equals(passwordEncoder.encode(oldPassword));
    }

    @Transactional
    public void updatePassword(String email, String password) throws UserEmailNotFoundException {
        findUserByEmail(email).setPassword(passwordEncoder.encode(password));
    }

    public void updateAvatar(MultipartFile avatar, String email) throws UserEmailNotFoundException {
        imgService.updateAvatarForUser(avatar, findUserByEmail(email));
    }

    private User findUserByEmail(String email) throws UserEmailNotFoundException {
        return userRepo.findByEmail(email).orElseThrow(UserEmailNotFoundException::new);
    }
}
