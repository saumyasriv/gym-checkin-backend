package com.gym.gym_checkin.controller;

import com.gym.gym_checkin.dto.ImportResponse;
import com.gym.gym_checkin.service.MigrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/migration")
@RequiredArgsConstructor
public class MigrationController {

    private final MigrationService migrationService;

    @PostMapping(
            value = "/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ImportResponse importCsv(
            @RequestParam("file") MultipartFile file
    ) {

        migrationService.importMembers(file);

        return new ImportResponse(
                "Import successful"
        );
    }
}
