package com.gym.gym_checkin.service;

import com.gym.gym_checkin.entity.Member;
import com.gym.gym_checkin.entity.Membership;
import com.gym.gym_checkin.repository.MemberRepository;
import com.gym.gym_checkin.repository.MembershipRepository;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MigrationService {

    private final MemberRepository memberRepository;

    private final MembershipRepository membershipRepository;

    public void importMembers(
            MultipartFile file
    ) {

        try {

            CSVReader reader = new CSVReader(
                    new InputStreamReader(
                            file.getInputStream()
                    )
            );

            List<String[]> rows = reader.readAll();

            for (int i = 1; i < rows.size(); i++) {

                String[] row = rows.get(i);

                Member member = new Member();

                member.setName(row[0]);
                member.setPhone(row[1]);
                member.setEmail(row[2]);

                Member savedMember =
                        memberRepository.save(member);

                Membership membership =
                        new Membership();

                membership.setMember(savedMember);

                membership.setTotalCredits(
                        Integer.parseInt(row[3])
                );

                membership.setRemainingCredits(
                        Integer.parseInt(row[4])
                );

                membership.setAmountPaid(
                        Double.parseDouble(row[5])
                );

                membership.setStartDate(
                        LocalDate.parse(row[6])
                );

                membership.setExpiryDate(
                        LocalDate.parse(row[7])
                );

                membership.setPackageName(row[8]);

                membershipRepository.save(membership);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
