package ru.medvedev.importer.dto.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class VtbOpeningCheckInn {

    private List<String> inns = new ArrayList<>();
}
