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

        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            // Disable foreign key checks
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

            for (String file : files) {
                executeSqlByLine(conn, file);
                System.out.println("[✔] Loaded SQL file: " + file);
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
                // 주석 무시
                if (line.trim().startsWith("--") || line.trim().isEmpty()) continue;
                sb.append(line).append(" ");
                if (line.trim().endsWith(";")) {
                    stmt.execute(sb.toString());
                    sb.setLength(0); // 리셋
                }
            }
        }
    }
}
