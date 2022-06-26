package ru.medvedev.importer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.medvedev.importer.dto.FieldNameVariantDto;
import ru.medvedev.importer.enums.XlsxRequireField;
import ru.medvedev.importer.service.FieldNameVariantService;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingController {

    private final FieldNameVariantService fieldNameVariantService;

    @GetMapping("/fields")
    public String getPage(Model model) {
        model.addAttribute("fieldsMap", fieldNameVariantService.getAll());
        model.addAttribute("fields", XlsxRequireField.values());
        return "fields_setting";
    }

    @PostMapping("/fields")
    @ResponseBody
    public ResponseEntity<?> saveFieldVariantNames(@RequestBody List<FieldNameVariantDto> list) throws IOException {

        fieldNameVariantService.create(list);
        return ResponseEntity.ok().build();
    }
}
