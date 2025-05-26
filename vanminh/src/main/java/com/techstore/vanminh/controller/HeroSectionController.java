package com.techstore.vanminh.controller;

import com.techstore.vanminh.dto.HeroSectionDTO;
import com.techstore.vanminh.dto.response.BaseResponse;
import com.techstore.vanminh.service.HeroSectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/hero")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HeroSectionController {

    private final HeroSectionService heroSectionService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<HeroSectionDTO> createHero(@ModelAttribute @Valid HeroSectionDTO heroDTO) {
        HeroSectionDTO created = heroSectionService.createHeroSection(heroDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<HeroSectionDTO> updateHero(
            @PathVariable Long id,
            @ModelAttribute @Valid HeroSectionDTO heroDTO) {
        HeroSectionDTO updated = heroSectionService.updateHeroSection(id, heroDTO);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<BaseResponse<HeroSectionDTO>> getAll(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize) {
        return ResponseEntity.ok(heroSectionService.getAllHeroSections(pageNumber, pageSize));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HeroSectionDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(heroSectionService.getHeroSectionById(id));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        heroSectionService.deleteHeroSection(id);
        return ResponseEntity.ok("Xoá hero section thành công");
    }

    @GetMapping("/image/{fileName}")
    public ResponseEntity<InputStreamResource> getImage(@PathVariable String fileName) throws IOException {
        InputStream stream = heroSectionService.getHeroImage(fileName);
        MediaType contentType = MediaType.IMAGE_JPEG; // hoặc suy đoán theo file extension

        return ResponseEntity.ok()
                .contentType(contentType)
                .body(new InputStreamResource(stream));
    }
}
