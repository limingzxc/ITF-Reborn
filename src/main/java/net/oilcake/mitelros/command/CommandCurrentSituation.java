package net.oilcake.mitelros.command;

import net.minecraft.*;

import java.util.List;

public class CommandCurrentSituation extends CommandBase {
    public String getCommandName() {
        return "getcurrentsituation";
    }

    public String getCommandUsage(ICommandSender iCommandListener) {
        return "commands.getcurrentsituation.usage";
    }

    public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] par2ArrayOfStr) {
        return (par2ArrayOfStr.length == 1) ? getListOfStringsMatchingLastWord(par2ArrayOfStr, new String[]{"insulin", "protein", "phytonutrients"}) : null;
    }

    public void processCommand(ICommandSender iCommandListener, String[] strings) {
        ServerPlayer player = getCommandSenderAsPlayer(iCommandListener);
        switch (strings[0]) {
            case "insulin":
                iCommandListener.sendChatToPlayer(ChatMessageComponent.createFromText("目前玩家的胰岛素抗性为" + player.getInsulinResistance()).setColor(EnumChatFormatting.WHITE));
                return;
            case "protein":
                iCommandListener.sendChatToPlayer(ChatMessageComponent.createFromText("目前玩家的蛋白质为" + player.getProtein()).setColor(EnumChatFormatting.WHITE));
                return;
            case "phytonutrients":
                iCommandListener.sendChatToPlayer(ChatMessageComponent.createFromText("目前玩家的植物营养素为" + player.getPhytonutrients()).setColor(EnumChatFormatting.WHITE));
                return;
        }
        iCommandListener.sendChatToPlayer(ChatMessageComponent.createFromText("用法:/getcurrentsituation <insulin|protein|phytonutrients>").setColor(EnumChatFormatting.RED));
    }
}
