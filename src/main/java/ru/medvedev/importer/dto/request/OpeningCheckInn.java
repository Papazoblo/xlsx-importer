package ru.medvedev.importer.dto.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OpeningCheckInn {

    private List<String> inns = new ArrayList<>();
}
