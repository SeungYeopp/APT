package com.ssafy.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SqlDumpRunner implements CommandLineRunner {

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Override
    public void run(String... args) throws Exception {
        String dbName = getDatabaseName(jdbcUrl);
        String[] files = {
                "aptdb_board.sql",
                "aptdb_chat_history.sql",
                "aptdb_dong_code.sql",
                "aptdb_file_info.sql",
                "aptdb_gugun_code.sql",
                "aptdb_house_deals.sql",
                "aptdb_house_infos.sql",
                "aptdb_interest_area.sql",
                "aptdb_interest_house.sql",
                "aptdb_oauth_entity.sql",
                "aptdb_recent_deals.sql",
                "aptdb_review.sql",
                "aptdb_sido_code.sql",
                "aptdb_user.sql",
                "aptdb_verification_code.sql"
        };

        for (String file : files) {
            String path = "src/main/resources/dump/" + file;
            String command = String.format(
                    "mysql -u%s -p%s %s < %s",
                    username, password, dbName, path
            );
            Process process = Runtime.getRuntime().exec(new String[] { "bash", "-c", command });
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Failed to execute dump: " + file);
            } else {
                System.out.println("[✔] Executed: " + file);
            }
        }
    }

    private String getDatabaseName(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }
}
