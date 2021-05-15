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

package fr.veridiangames.server;

import java.util.Random;
import java.util.Scanner;

import fr.veridiangames.core.GameCore;
import fr.veridiangames.core.game.entities.player.NetworkedPlayer;
import fr.veridiangames.core.maths.Quat;
import fr.veridiangames.core.maths.Vec3;
import fr.veridiangames.core.network.NetworkableServer;
import fr.veridiangames.core.utils.Indexer;
import fr.veridiangames.server.server.Configuration;
import fr.veridiangames.server.server.NetworkServer;

import static java.lang.Math.abs;

/**
 * Created by Marccspro on 31 janv. 2016.
 */
public class ServerMain
{
	private Scanner 		scanner;
	private NetworkServer 	server;
	private GameCore 		core;
	private Configuration 	config;

	private static ServerMain instance;

	public ServerMain(int port)
	{
		this.config = new Configuration();
		config.load("ubercube.cfg");

		instance = this;

		this.scanner = new Scanner(System.in);
		this.core = new GameCore();
		this.core.getGame().createWorld(abs(new Random().nextInt()));

		this.server = new NetworkServer(port, scanner, core);
	}

	public ServerMain(int port, String filePath)
	{
		this.config = new Configuration();
		config.load(filePath);

		instance = this;

		this.scanner = new Scanner(System.in);
		this.core = new GameCore();
		this.core.getGame().loadWorld(config.get("mapPath"));

		this.server = new NetworkServer(port, scanner, core);
	}

	public NetworkServer getNet()
	{
		return server;
	}

	public static ServerMain getInstance()
	{
		return instance;
	}

	public static void main(String[] args)
	{
		if ( args.length == 1)
			new ServerMain(Integer.parseInt(args[0]));
		else if ( args.length == 2)
			new ServerMain(Integer.parseInt(args[0]), args[1]);
		else
			System.out.println("Usage: ./ubercube_server port [configFile]");
	}
}