package cli;

import service.ConversionRuleCollectionManager;
import service.ConversionService;
import service.UnitCollectionManager;

import java.util.Scanner;

public record CommandContext(
        Scanner scanner,
        UnitCollectionManager unitManager,
        ConversionRuleCollectionManager ruleManager,
        ConversionService conversionService
) {
}
