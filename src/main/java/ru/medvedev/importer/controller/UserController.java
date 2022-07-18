package ru.medvedev.importer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.medvedev.importer.dto.PagingDto;
import ru.medvedev.importer.dto.PermissionDto;
import ru.medvedev.importer.dto.UserDto;
import ru.medvedev.importer.dto.UserInput;
import ru.medvedev.importer.service.UserService;

import java.util.List;

import static ru.medvedev.importer.utils.SecurityUtils.getAuthorityList;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping
    public String getPage(Model model,
                          @RequestParam(value = "page", defaultValue = "1") int page,
                          @RequestParam(value = "size", defaultValue = "15") int size) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("createAt")));

        Page<UserDto> resultPage = service.getPage(pageable);
        model.addAttribute("users", resultPage);
        model.addAttribute("authority", getAuthorityList());
        model.addAttribute("paging", PagingDto.of(resultPage.getTotalPages(), page, size));
        return "users";
    }

    @GetMapping("/update")
    public String updateUser(Model model, @RequestParam(value = "id", required = false) Long id) {

        model.addAttribute("user", id == null ? null : service.getById(id));
        model.addAttribute("authority", getAuthorityList());
        model.addAttribute("permissions", service.getPermissionList());
        return "add_user";
    }

    @GetMapping("/permissions")
    @ResponseBody
    public List<PermissionDto> getPermissionList() {
        return service.getPermissionList();
    }

    @PostMapping
    public String create(@RequestBody UserInput input) {
        service.create(input);
        return "redirect:/users";
    }

    @PutMapping("/{id}")
    @ResponseBody
    public String update(@PathVariable("id") Long id, @RequestBody UserInput input) {
        service.update(id, input);
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping("/{id}/lock/{page}")
    public String blockUser(@PathVariable("id") Long id,
                            @PathVariable("page") int page) {
        service.blockUser(id);
        return "redirect:/users?page=" + page;
    }

    @GetMapping("/{id}/unlock/{page}")
    public String unblockUser(@PathVariable("id") Long id,
                              @PathVariable("page") int page) {
        service.unblockUser(id);
        return "redirect:/users?page=" + page;
    }
}
