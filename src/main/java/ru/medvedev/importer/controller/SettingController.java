package ru.medvedev.importer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.medvedev.importer.dto.AutoLinkXlsxFieldDto;
import ru.medvedev.importer.dto.FieldNameVariantDto;
import ru.medvedev.importer.enums.SkorozvonField;
import ru.medvedev.importer.enums.XlsxRequireField;
import ru.medvedev.importer.service.AutoLinkXlsxFieldService;
import ru.medvedev.importer.service.FieldNameVariantService;

import java.util.Arrays;
import java.util.List;

import static ru.medvedev.importer.utils.SecurityUtils.getAuthorityList;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingController {

    private final FieldNameVariantService fieldNameVariantService;
    private final AutoLinkXlsxFieldService autoLinkXlsxFieldService;

    @GetMapping("/fields")
    public String getPageField(Model model) {
        model.addAttribute("fieldsMap", fieldNameVariantService.getAll());
        model.addAttribute("fields", Arrays.stream(XlsxRequireField.values())
                .filter(field -> field != XlsxRequireField.FIO)
                .toArray());
        model.addAttribute("authority", getAuthorityList());
        return "fields_setting";
    }

    @PostMapping("/fields")
    @ResponseBody
    public ResponseEntity<?> saveFieldVariantNames(@RequestBody List<FieldNameVariantDto> list) {

        fieldNameVariantService.create(list);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/auto-link")
    public String getPageAutoLink(Model model) {
        model.addAttribute("fieldsMap", autoLinkXlsxFieldService.getAll());
        model.addAttribute("fields", SkorozvonField.selectValues());
        model.addAttribute("authority", getAuthorityList());
        return "auto_link_field";
    }

    @PostMapping("/auto-link")
    @ResponseBody
    public ResponseEntity<?> saveAutoLinkVariantNames(@RequestBody List<AutoLinkXlsxFieldDto> list) {

        autoLinkXlsxFieldService.create(list);
        return ResponseEntity.ok().build();
    }
}
