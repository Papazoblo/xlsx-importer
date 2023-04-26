package ru.medvedev.importer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import ru.medvedev.importer.dto.InnExcludeFilter;
import ru.medvedev.importer.entity.ContactNewEntity;
import ru.medvedev.importer.entity.InnExcludeEntity;
import ru.medvedev.importer.entity.id.InnExcludeId;
import ru.medvedev.importer.enums.Bank;
import ru.medvedev.importer.repository.InnExcludeRepository;
import ru.medvedev.importer.service.sheethandler.ContactNewSheetHandler;
import ru.medvedev.importer.service.sheethandler.InnExcludeSheetHandler;
import ru.medvedev.importer.specification.InnExcludeSpecification;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class InnExcludeService {

    private final InnExcludeRepository repository;

    public Page<InnExcludeEntity> getAll(InnExcludeFilter filter, Pageable pageable) {
        return repository.findAll(InnExcludeSpecification.of(filter), pageable);
    }

    public void create(MultipartFile file, Bank bank) {
        create(readInnList(file, bank), bank);
    }

    public void create(Collection<String> innList, Bank bank) {
        Map<String, InnExcludeEntity> map = repository.findAllById_Bank(bank).stream()
                .collect(Collectors.toMap(item -> item.getId().getInn(), item -> item));
        repository.saveAll(innList.stream()
                .filter(inn -> !map.containsKey(inn))
                .map(inn -> {
                    InnExcludeId id = new InnExcludeId();
                    id.setInn(inn);
                    id.setBank(bank);
                    InnExcludeEntity entity = new InnExcludeEntity();
                    entity.setId(id);
                    return entity;
                })
                .collect(Collectors.toList()));
    }

    public void delete(MultipartFile file, Bank bank) {
        deleteByInnListAndBank(new ArrayList<>(readInnList(file, bank)), bank);
    }

    @Transactional
    public void deleteByInnListAndBank(ArrayList<String> innList, Bank bank) {

        int MAX_BATCH_SIZE = 1000;
        for (int i = 0; i < innList.size(); i += MAX_BATCH_SIZE) {
            repository.deleteAllById_BankAndId_InnIn(bank, innList.subList(i,
                    Math.min((i + MAX_BATCH_SIZE), innList.size())));
        }
    }

    public void deleteByBank(Bank bank) {
        repository.deleteAllById_Bank(bank);
    }

    public Set<String> readInnList(MultipartFile file, Bank bank) {
        try (InputStream inputStream = file.getInputStream()) {

            OPCPackage pkg = OPCPackage.open(inputStream);
            XSSFReader r = new XSSFReader(pkg);
            SharedStringsTable sst = (SharedStringsTable) r.getSharedStringsTable();
            XMLReader parser = XMLHelper.newXMLReader();
            InnExcludeSheetHandler handler = new InnExcludeSheetHandler(sst);
            parser.setContentHandler(handler);
            readDataFromSheet(parser, r);
            return handler.getInnList();
        } catch (Exception e) {
            log.error("Cannot read excel-file '{}'", file.getName(), e);
            return Collections.emptySet();
        }
    }

    public static void readDataFromSheet(XMLReader parser, XSSFReader reader) throws IOException, SAXException, InvalidFormatException {
        Iterator<InputStream> sheets = reader.getSheetsData();
        int i = 0;
        while (sheets.hasNext()) {
            if (i > 0) {
                return;
            }
            System.out.println("Start process sheet");
            InputStream sheet = sheets.next();
            InputSource sheetSource = new InputSource(sheet);
            parser.parse(sheetSource);
            sheet.close();
            System.out.println("End process sheet");
            i++;
        }
    }
}
