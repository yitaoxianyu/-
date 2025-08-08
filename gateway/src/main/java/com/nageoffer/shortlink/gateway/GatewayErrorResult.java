package com.nageoffer.shortlink.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayErrorResult {

    private String message;

    private int status;


}
