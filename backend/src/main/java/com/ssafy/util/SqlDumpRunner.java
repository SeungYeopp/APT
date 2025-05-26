package com.ssafy.util;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;

@Component
@RequiredArgsConstructor
public class SqlDumpRunner implements CommandLineRunner {

    private final DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        String[] files = {
                "aptdb_sido_code.sql",
                "aptdb_gugun_code.sql",
                "aptdb_dong_code.sql",
                "aptdb_user.sql",
                "aptdb_house_infos.sql",
                "aptdb_house_deals.sql",
                "aptdb_recent_deals.sql",
                "aptdb_review.sql",
                "aptdb_board.sql",
                "aptdb_file_info.sql",
                "aptdb_interest_area.sql",
                "aptdb_interest_house.sql",
                "aptdb_oauth_entity.sql",
                "aptdb_chat_history.sql",
                "aptdb_verification_code.sql"
        };

        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            // 강제로 세션 문자셋 설정
            stmt.execute("SET NAMES utf8mb4");
            stmt.execute("SET character_set_connection = utf8mb4");
            stmt.execute("SET character_set_client = utf8mb4");
            stmt.execute("SET character_set_results = utf8mb4");

            // Disable foreign key checks
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

            for (String file : files) {
                try {
                    executeSqlByLine(conn, file);
                    System.out.println("[✔] Loaded SQL file: " + file);
                } catch (Exception e) {
                    System.err.println("[✘] Failed to load: " + file);
                    e.printStackTrace();
                    throw e;
                }
            }

            // Enable foreign key checks
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    private void executeSqlByLine(Connection conn, String filename) throws Exception {
        var resource = new ClassPathResource("dump/" + filename);
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
                Statement stmt = conn.createStatement()
        ) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // 주석과 빈 줄은 무시
                if (line.startsWith("--") || line.isEmpty()) continue;

                // 문자셋/Collate 통일 (강제 변환)
                line = line.replace("CHARACTER SET utf8mb3 COLLATE utf8mb3_bin", "CHARACTER SET utf8mb4 COLLATE utf8mb4_bin");
                line = line.replace("DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin", "DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin");
                line = line.replace("utf8mb3", "utf8mb4");
                line = line.replace("utf8mb3_bin", "utf8mb4_bin");
                line = line.replace("utf8mb4_general_ci", "utf8mb4_bin");

                sb.append(line).append(" ");
                if (line.endsWith(";")) {
                    stmt.execute(sb.toString());
                    sb.setLength(0); // 리셋
                }
            }
        }
    }
}
