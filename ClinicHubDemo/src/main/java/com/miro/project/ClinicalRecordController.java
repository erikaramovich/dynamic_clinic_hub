package com.miro.project;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patients")
@Tag(name = "Clinical Records Service")
public class ClinicalRecordController {

    @GetMapping("/{patientId}/records")
    public String getPatientRecords(@PathVariable Long patientId) {
        return null;
    }

    @PostMapping("/{patientId}/records")
    public String addClinicalNote(@PathVariable Long patientId) {
        return null;
    }

    @PostMapping("/{patientId}/files")
    public String uploadMedicalFile(@PathVariable Long patientId) {
        return null;
    }

    @GetMapping("/{patientId}/files/{fileId}")
    public String getMedicalFileDownloadLink(@PathVariable Long patientId, @PathVariable String fileId) {
        return null;
    }
}