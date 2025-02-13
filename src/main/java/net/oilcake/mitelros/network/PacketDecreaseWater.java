package net.oilcake.mitelros.network;

import net.minecraft.NetHandler;
import net.minecraft.Packet;
import net.oilcake.mitelros.api.ITFNetHandler;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class PacketDecreaseWater extends Packet {
    public float hungerWater;

    public PacketDecreaseWater(float hungerWater) {
        this.hungerWater = hungerWater;
    }

    public void readPacketData(DataInput par1DataInput) throws IOException {
        this.hungerWater = par1DataInput.readFloat();
    }

    public void writePacketData(DataOutput par1DataOutput) throws IOException {
        par1DataOutput.writeFloat(this.hungerWater);
    }

    public void processPacket(NetHandler net_handler) {
        ((ITFNetHandler) net_handler).handleDecreaseWater(this);
    }

    public int getPacketSize() {
        return 4;
    }
}
