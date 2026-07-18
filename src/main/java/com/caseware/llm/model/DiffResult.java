package com.caseware.llm.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiffResult {
    private List<String> added;
    private List<String> modified;
    private List<String> removed;
}
