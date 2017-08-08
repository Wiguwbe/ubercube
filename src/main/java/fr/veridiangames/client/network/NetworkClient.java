/*
 * Copyright (C) 2016 Team Ubercube
 *
 * This file is part of Ubercube.
 *
 *     Ubercube is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Ubercube is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Ubercube.  If not, see http://www.gnu.org/licenses/.
 */

package fr.veridiangames.client.network;

import java.net.InetAddress;
import java.net.UnknownHostException;

import fr.veridiangames.client.audio.AudioSystem;
import fr.veridiangames.client.main.screens.ConsoleScreen;
import fr.veridiangames.core.GameCore;
import fr.veridiangames.core.game.entities.audio.AudioSource;
import fr.veridiangames.core.network.NetworkableClient;
import fr.veridiangames.core.network.packets.Packet;
import fr.veridiangames.core.utils.DataBuffer;
import fr.veridiangames.client.Ubercube;

/**
 * Created by Marccspro on 24 f�vr. 2016.
 */
public class NetworkClient implements NetworkableClient
{
	private int 			port;
	private InetAddress 	address;
	private boolean 		connected;

	private NetworkClientTCP tcp;
	private NetworkClientUDP udp;

	private int id;

	public NetworkClient(int id, String address, int port, Ubercube ubercube)
	{
		try
		{
			this.address = InetAddress.getByName(address);
			this.port = port;

			this.id = id;
			this.tcp = new NetworkClientTCP(this, address, port);
			this.udp = new NetworkClientUDP(this, address, port);
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
	}
	
	public void tcpSend(Packet packet)
	{
		tcp.send(packet);
	}

	public void udpSend(Packet packet)
	{
		udp.send(packet.getData().getData());
	}
	
	public void stop()
	{
		log("Network has terminated !");
		System.exit(0);
	}

	public void log(String msg)
	{
		if (msg.toLowerCase().contains("error"))
			System.err.println(msg);
		else
			System.out.println(msg);
	}

	public void console(String msg)
	{
		ConsoleScreen console = Ubercube.getInstance().getConsole();
		if (console != null)
			console.log(msg);
	}

	public void playSound(AudioSource audioSource)
	{
		AudioSystem.play(audioSource);
	}

	public void setConnected(boolean connected)
	{
		this.connected = connected;
	}

	public boolean isConnected()
	{
		return connected;
	}

	public int getPort()
	{
		return port;
	}

	public GameCore getCore()
	{
		return GameCore.getInstance();
	}

	public InetAddress getAddress()
	{
		return address;
	}

	public NetworkClientTCP getTcp()
	{
		return tcp;
	}

	public NetworkClientUDP getUdp()
	{
		return udp;
	}

	public int getID() {
		return id;
	}
}