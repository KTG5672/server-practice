package kr.hhplus.be.server.point.application;

import kr.hhplus.be.server.user.domain.exception.UserNotFoundException;
import kr.hhplus.be.server.user.domain.model.User;
import kr.hhplus.be.server.user.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PointChargeService {

    private final UserRepository userRepository;

    public PointChargeService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void chargePoint(String userId, long chargePoint) {
        User user = userRepository.findById(userId).orElseThrow(() ->
            new UserNotFoundException(userId));
        user.chargePoint(chargePoint);
        userRepository.save(user);
    }

}
