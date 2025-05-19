package com.ssafy.user.mapper;

import com.ssafy.user.vo.User;
import com.ssafy.user.vo.VerificationCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {
    void registerUser(User user);
    User getUserByEmail(@Param("email") String email);
    User getUserById(@Param("id") String id);
    void updateUser(User user);

    void saveVerificationCode(VerificationCode code);
    VerificationCode getVerificationCodeByEmail(@Param("email") String email);
    void deleteVerificationCodeByEmail(@Param("email") String email);
    Integer getMaxUserId();

    @Select("SELECT id FROM user WHERE id = #{kakaoId}")
    String findUserIdByKakaoId(String kakaoId);  // 새로운 메서드 추가

    @Select("SELECT * FROM user where email = #{email}")
    User findUserIdByEmail(String email);

    @Update("UPDATE user SET profile_image = #{profileImage} WHERE id = #{id}")
    void updateProfileImage(@Param("id") String id, @Param("profileImage") String profileImage);

    void updateUser2(@Param("id") String id, @Param("nickname") String nickname, @Param("password") String password, @Param("salt") String salt);


}
