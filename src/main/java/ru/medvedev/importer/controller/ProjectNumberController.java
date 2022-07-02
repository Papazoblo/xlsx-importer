package ru.medvedev.importer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.medvedev.importer.dto.PagingDto;
import ru.medvedev.importer.dto.ProjectNumberDto;
import ru.medvedev.importer.service.ProjectNumberService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/settings/projects")
public class ProjectNumberController {

    private final ProjectNumberService projectNumberService;

    @GetMapping
    public String getPageProject(Model model,
                                 @RequestParam(value = "page", defaultValue = "1") int page,
                                 @RequestParam(value = "size", defaultValue = "15") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("date")));

        Page<ProjectNumberDto> resultPage = projectNumberService.getPage(pageable);
        model.addAttribute("projectNumbers", resultPage);
        model.addAttribute("paging", PagingDto.of(resultPage.getTotalPages(), page, size));
        return "project_number_setting";
    }

    @PostMapping
    public String create(@RequestBody ProjectNumberDto input) {
        projectNumberService.create(input);
        return "redirect:/settings/projects";
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public void delete(@PathVariable("id") Long id) {
        projectNumberService.delete(id);
    }
}
