package com.example.config;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * 
 * <b>MappingConfig</b><br>
 * 
 * Centered MapStruct configuration used by all mappers.
 */
@MapperConfig(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.WARN,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public class MappingConfig { }
