package net.oilcake.mitelros.config;

import fi.dy.masa.malilib.config.SimpleConfigs;
import fi.dy.masa.malilib.config.interfaces.IConfigResettable;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.gui.button.GuiButtonCommented;
import fi.dy.masa.malilib.gui.screen.GuiScreenCommented;
import fi.dy.masa.malilib.gui.screen.ValueScreen;
import net.minecraft.GuiButton;
import net.minecraft.GuiScreen;
import net.minecraft.GuiYesNoMITE;
import net.minecraft.I18n;

public class ChallengeScreen extends GuiScreenCommented {

    private GuiScreen parentScreen;
    private final SimpleConfigs configs;

    public ChallengeScreen(GuiScreen parentScreen) {
        super("挑战设置");
        this.parentScreen = parentScreen;
        this.configs = ITFConfig.getInstance();
    }

    public void initGui() {
        this.buttonList.clear();

        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 6 + 24, "自然恶意"));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 6 + 48, "疯狂劲敌"));
        this.buttonList.add(new GuiButton(2, this.width / 2 - 100, this.height / 6 + 72, "天赐福星"));
        this.buttonList.add(new GuiButtonCommented(3, this.width / 2 - 100, this.height / 6 + 120, "重置全部挑战设置", "仅重置挑战设置, 而不影响实验性玩法等"));
        this.buttonList.add(new GuiButton(4, this.width / 2 - 100, this.height / 6 + 96, "启用终极挑战"));

        this.buttonList.add(new GuiButton(200, this.width / 2 - 100, this.height / 6 + 168, I18n.getString("gui.done")));
    }

    protected void actionPerformed(GuiButton par1GuiButton) {
        GuiYesNoMITE var3;
        switch (par1GuiButton.id) {
            case 0:
                this.mc.displayGuiScreen(new ValueScreen(this, "自然恶意", this.configs.setValues(ITFConfig.spite)));
                break;
            case 1:
                this.mc.displayGuiScreen(new ValueScreen(this, "数值设置", this.configs.setValues(ITFConfig.enemy)));
                break;
            case 2:
                this.mc.displayGuiScreen(new ValueScreen(this, "数值设置", this.configs.setValues(ITFConfig.luck)));
                break;
            case 3:
                var3 = new GuiYesNoMITE(this, "真的要重置全部挑战设置吗?", this.configs.getName(), "是", "否", 3);
                this.mc.displayGuiScreen(var3);
                break;
            case 4:
                var3 = new GuiYesNoMITE(this, "真的要启用终极挑战吗?", this.configs.getName(), "是", "否", 4);
                this.mc.displayGuiScreen(var3);
                break;
            case 200:
                this.mc.displayGuiScreen(this.parentScreen);
        }

    }

    public void confirmClicked(boolean par1, int par2) {
        if (par1) {
            if (par2 == 3) {
                ITFConfig.challenge.forEach(IConfigResettable::resetToDefault);
                this.configs.save();
            }
            if (par2 == 4) {
                ITFConfig.spite.forEach(x -> {
                    if (x instanceof ConfigBooleanChallenge challenge) challenge.setBooleanValue(true);
                    if (x instanceof ConfigInteger configInteger)
                        configInteger.setIntegerValue(configInteger.getMaxIntegerValue());
                });
                ITFConfig.enemy.forEach(x -> {
                    if (x instanceof ConfigBooleanChallenge challenge) challenge.setBooleanValue(true);
                    if (x instanceof ConfigInteger configInteger)
                        configInteger.setIntegerValue(configInteger.getMaxIntegerValue());
                });
                ITFConfig.luck.forEach(x -> {
                    if (x instanceof ConfigBooleanChallenge challenge) challenge.setBooleanValue(false);
                    if (x instanceof ConfigInteger configInteger) configInteger.setIntegerValue(0);
                });
                this.configs.save();
            }
        }
        this.mc.displayGuiScreen(this);
    }
}
