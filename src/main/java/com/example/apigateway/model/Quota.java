package com.example.apigateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Quota  implements Serializable {
    private int maxRequests;
    private int period;
    private String unit;
}
