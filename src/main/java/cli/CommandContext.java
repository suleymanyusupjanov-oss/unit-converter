package cli;

import main.java.service.ConversionRuleCollectionManager;
import main.java.service.ConversionService;
import main.java.service.UnitCollectionManager;
import java.util.Scanner;
public record CommandContext(
        Scanner scanner,
        UnitCollectionManager unitManager,
        ConversionRuleCollectionManager ruleManager,
        ConversionService conversionService
) {
}
