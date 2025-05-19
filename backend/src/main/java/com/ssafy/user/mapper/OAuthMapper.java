package com.ssafy.user.mapper;

import com.ssafy.user.vo.OAuthEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OAuthMapper {

    int existKakaoUser(@Param("kakaoId") String kakaoId);

    void insertKakaoUser(OAuthEntity oauthEntity);

    @Select("SELECT * FROM oauth_entity WHERE user_id = #{userId}")
    OAuthEntity findOAuthByUserId(String userId);  // 새로운 메서드 추가

    @Update("UPDATE oauth_entity SET access_token = #{accessToken}, refresh_token = #{refreshToken} WHERE user_id = #{userId}")
    void updateKakaoUser(@Param("accessToken") String accessToken, @Param("refreshToken") String refreshToken, @Param("userId") String userId);
    
}
