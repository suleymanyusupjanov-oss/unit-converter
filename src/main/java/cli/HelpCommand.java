package cli;

import java.util.Map;

public class HelpCommand extends Command {
    private final Map<String, Command> commands;

    public HelpCommand(Map<String, Command> commands) {
        super("help", "Вывести список всех команд");
        this.commands = commands;
    }

    @Override
    public void execute(String[] args, CommandContext context) {
        for (Command cmd : commands.values()) {
            System.out.printf("%-20s - %s%n", cmd.getName(), cmd.getDescription());
        }
    }
}
