package ru.medvedev.importer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.medvedev.importer.dto.FieldNameVariantDto;
import ru.medvedev.importer.enums.XlsxRequireField;
import ru.medvedev.importer.service.FieldNameVariantService;

import java.util.List;

import static ru.medvedev.importer.utils.SecurityUtils.getAuthorityList;

@Controller
@RequestMapping("/settings/fields")
@RequiredArgsConstructor
public class SettingController {

    private final FieldNameVariantService fieldNameVariantService;

    @GetMapping
    public String getPageField(Model model) {
        model.addAttribute("fieldsMap", fieldNameVariantService.getAll());
        model.addAttribute("fields", XlsxRequireField.values());
        model.addAttribute("authority", getAuthorityList());
        return "fields_setting";
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<?> saveFieldVariantNames(@RequestBody List<FieldNameVariantDto> list) {

        fieldNameVariantService.create(list);
        return ResponseEntity.ok().build();
    }
}
