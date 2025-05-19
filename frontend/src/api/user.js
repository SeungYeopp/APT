import axios from "@/util/axios-common.js";

function login(user, success, fail) {
    axios
        .post(`/user/login/email`, {
            email: user.email,
            password: user.password
        })
        .then(success)
        .catch(fail);
}

// 로그아웃 함수
function logout(success, fail) {
    axios
        .post(
            `/user/logout`,
            {},
            {
                withCredentials: true, // 쿠키를 전송하기 위해 설정
            }
        )
        .then(success)
        .catch(fail);
}

export { login, logout };