package ru.medvedev.importer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.medvedev.importer.service.XlsxParserService;

import java.util.Arrays;
import java.util.Collections;

@Controller
@RequiredArgsConstructor
@RequestMapping()
@Log4j2
public class ImportController {

    private final XlsxParserService xlsxParserService;

    @GetMapping
    public String importXlsx(Model model) {
        model.addAttribute("test", "Test");
        try {
            model.addAttribute("headers", xlsxParserService.readColumnHeaders());
        } catch (Exception ex) {
            log.info("Error read xlsx", ex);
        }
        return "main";
    }

    @PostMapping("/import/xlsx")
    public String redirect(@RequestParam("collage") MultipartFile[] files,
                           RedirectAttributes redirectAttr) {

        Collections.shuffle(Arrays.asList(files));
        redirectAttr.addFlashAttribute("pictures", files);
        return "redirect:/";
    }
}
