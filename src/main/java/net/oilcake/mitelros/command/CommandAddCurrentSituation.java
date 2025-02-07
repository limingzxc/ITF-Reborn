package net.oilcake.mitelros.command;

import net.minecraft.*;

import java.util.List;

public class CommandAddCurrentSituation extends CommandBase {
    public String getCommandName() {
        return "addcurrentsituation";
    }

    public String getCommandUsage(ICommandSender iCommandListener) {
        return "commands.addcurrentsituation.usage";
    }

    public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] par2ArrayOfStr) {
        return (par2ArrayOfStr.length == 1) ? getListOfStringsMatchingLastWord(par2ArrayOfStr, new String[]{"insulin", "protein", "phytonutrients"}) : null;
    }

    public void processCommand(ICommandSender iCommandListener, String[] strings) {
        ServerPlayer player = getCommandSenderAsPlayer(iCommandListener);
        if (strings[1] != null) {
            switch (strings[0]) {
                case "insulin":
                    player.addInsulinResistance(Integer.parseInt(strings[1]));
                    iCommandListener.sendChatToPlayer(ChatMessageComponent.createFromText("目前玩家的胰岛素抗性为" + player.getInsulinResistance()).setColor(EnumChatFormatting.WHITE));
                    return;
                case "protein":
                    player.addProtein(Integer.parseInt(strings[1]));
                    iCommandListener.sendChatToPlayer(ChatMessageComponent.createFromText("目前玩家的蛋白质为" + player.getProtein()).setColor(EnumChatFormatting.WHITE));
                    return;
                case "phytonutrients":
                    player.addPhytonutrients(Integer.parseInt(strings[1]));
                    iCommandListener.sendChatToPlayer(ChatMessageComponent.createFromText("目前玩家的植物营养素为" + player.getPhytonutrients()).setColor(EnumChatFormatting.WHITE));
                    return;
            }
            iCommandListener.sendChatToPlayer(ChatMessageComponent.createFromText("用法:/addcurrentsituation <insulin|protein|phytonutrients> <num>").setColor(EnumChatFormatting.RED));
        } else {
            iCommandListener.sendChatToPlayer(ChatMessageComponent.createFromText("用法:/addcurrentsituation <insulin|protein|phytonutrients> <num>").setColor(EnumChatFormatting.RED));
        }
    }
}
