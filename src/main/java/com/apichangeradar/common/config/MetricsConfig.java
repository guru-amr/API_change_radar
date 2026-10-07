package com.apichangeradar.common.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {
    @Bean
    public Counter specCheckCounter(MeterRegistry reg) {
        return Counter.builder("api_change_radar.spec_checks.total").register(reg);
    }
    @Bean
    public Counter changeDetectedCounter(MeterRegistry reg) {
        return Counter.builder("api_change_radar.changes_detected.total").register(reg);
    }
}

