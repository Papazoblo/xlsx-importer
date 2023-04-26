package ru.medvedev.importer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.medvedev.importer.dto.InnExcludeDto;
import ru.medvedev.importer.dto.InnExcludeFilter;
import ru.medvedev.importer.dto.PagingDto;
import ru.medvedev.importer.entity.InnExcludeEntity;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.service.InnExcludeService;

import java.util.stream.Collectors;

import static ru.medvedev.importer.utils.SecurityUtils.getAuthorityList;

@Controller
@RequestMapping("/inn-exclude")
@RequiredArgsConstructor
public class InnExcludeController {

    private final InnExcludeService innExcludeService;

    @GetMapping
    public String getExcludePage(Model model,
                                 @RequestParam(value = "page", defaultValue = "1") int page,
                                 @RequestParam(value = "size", defaultValue = "30") int size,
                                 InnExcludeFilter filter) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("createDate")));
        Page<InnExcludeEntity> resultPage = innExcludeService.getAll(filter, pageable);
        model.addAttribute("list", new PageImpl<>(resultPage.getContent().stream()
                .map(InnExcludeDto::of)
                .collect(Collectors.toList()), PageRequest.of(page, size), resultPage.getTotalPages()));
        model.addAttribute("filter", filter);
        model.addAttribute("banks", Bank.valuesNotExclude());
        model.addAttribute("authority", getAuthorityList());
        model.addAttribute("paging", PagingDto.of(resultPage.getTotalPages(), page, size));

        return "exclude_inn";
    }

    @PostMapping("/{bank}")
    @ResponseBody
    public ResponseEntity<?> create(@PathVariable("bank") Bank bank, @RequestParam("file") MultipartFile file) {
        innExcludeService.create(file, bank);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{bank}")
    @ResponseBody
    public ResponseEntity<?> delete(@PathVariable("bank") Bank bank, @RequestParam("file") MultipartFile file) {
        innExcludeService.delete(file, bank);
        return ResponseEntity.ok().build();
    }
}
