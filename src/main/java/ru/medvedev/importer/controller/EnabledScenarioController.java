package ru.medvedev.importer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.medvedev.importer.dto.EnabledScenarioDto;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.service.EnabledScenarioService;

import java.util.Comparator;
import java.util.List;

import static ru.medvedev.importer.utils.SecurityUtils.getAuthorityList;

@RequiredArgsConstructor
@Controller
@RequestMapping("/settings/enabled-scenario")
public class EnabledScenarioController {

    private final EnabledScenarioService service;

    @GetMapping
    public String getAll(Model model) {

        List<EnabledScenarioDto> dto = service.getAll();
        dto.sort(Comparator.comparing(EnabledScenarioDto::getScenarioId));

        model.addAttribute("scenarios", dto);
        model.addAttribute("banks", Bank.values());
        model.addAttribute("authority", getAuthorityList());
        return "enabled_scenarios";
    }

    @PostMapping
    public String create(@RequestBody EnabledScenarioDto input) {
        service.create(input);
        return "redirect:/settings/enabled-scenario";
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public void delete(@PathVariable("id") Long id) {
        service.deleteById(id);
    }
}
