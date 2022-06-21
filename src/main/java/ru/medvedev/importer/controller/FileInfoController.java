package ru.medvedev.importer.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.medvedev.importer.dto.FileInfoDto;
import ru.medvedev.importer.dto.PagingDto;
import ru.medvedev.importer.entity.FileInfoEntity;
import ru.medvedev.importer.service.FileDownloaderService;
import ru.medvedev.importer.service.FileInfoService;

import java.io.File;
import java.io.IOException;

@Controller
@RequestMapping("/file-storage")
@RequiredArgsConstructor
public class FileInfoController {

    private final FileInfoService service;
    private final FileDownloaderService fileDownloaderService;

    @GetMapping
    public String getPage(Model model,
                          @RequestParam(value = "page", defaultValue = "1") int page,
                          @RequestParam(value = "size", defaultValue = "15") int size) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("createAt")));

        Page<FileInfoDto> resultPage = service.getPage(pageable);
        model.addAttribute("files", resultPage);
        model.addAttribute("paging", PagingDto.of(resultPage.getTotalPages(), page, size));
        return "file_storage";
    }

    @GetMapping("/download/{id}")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long id) throws IOException {

        FileInfoEntity file = fileDownloaderService.download(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, String.format("inline; filename=\"%s\"", file.getName()));
        ResponseEntity<Resource> response = ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(file.getType()))
                .body(new ByteArrayResource(FileUtils.readFileToByteArray(new File(file.getPath()))));
        new File(file.getPath()).delete();
        return response;
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public void delete(@PathVariable("id") Long id) {
        service.delete(id);
    }
}
