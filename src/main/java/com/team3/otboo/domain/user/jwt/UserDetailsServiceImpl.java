package com.team3.otboo.domain.user.jwt;

import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.mapper.UserMapper;
import com.team3.otboo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    @Override
    // username으로 DB에서 user를 불러온다.
    // 그리고 userDetail을 생성해서 돌려주는 메서드
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return new UserDetailsImpl(userMapper.toDto(user), user.getPassword());
    }
}
