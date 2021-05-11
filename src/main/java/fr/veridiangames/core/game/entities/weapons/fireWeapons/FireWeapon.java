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

package fr.veridiangames.core.game.entities.weapons.fireWeapons;

import fr.veridiangames.client.Ubercube;
import fr.veridiangames.core.GameCore;
import fr.veridiangames.core.audio.Sound;
import fr.veridiangames.core.game.entities.audio.AudioSource;
import fr.veridiangames.core.game.entities.bullets.Bullet;
import fr.veridiangames.core.game.entities.components.ECKeyMovement;
import fr.veridiangames.core.game.entities.player.ClientPlayer;
import fr.veridiangames.core.game.entities.weapons.Weapon;
import fr.veridiangames.core.maths.Mathf;
import fr.veridiangames.core.maths.Transform;
import fr.veridiangames.core.maths.Vec3;
import fr.veridiangames.core.network.Protocol;
import fr.veridiangames.core.network.packets.BulletShootPacket;
import fr.veridiangames.core.network.packets.SoundPacket;
import fr.veridiangames.core.utils.Indexer;
import fr.veridiangames.server.server.NetworkServer;

public class FireWeapon extends Weapon
{
	private int fireFrequency = 10; // per seconds
	private int reloadTime = 60;	// time to reload, sixtieths of second (1/60) seconds
	
	private Transform shootPoint;
	private boolean shooting;
	private boolean shot;
	private boolean reloading = false;
	private float shootForce = 0.2f;
	private float shootPecision = 0.5f;
	private float shootPecisionIdle = 0.5f;
	private float shootPecisionZoomed = 0.5f;
	private float recoil = 1f;
	private float recoilOnZoom = 1f;
	private int damage = 20;

	private int shootTimer = 0;
	private int reloadTimer = 0;

	private int maxBullets;
	private int bulletsLeft;

	private float audioGain;
	
	public FireWeapon(int id, int model)
	{
		super(id, model);
		this.shootPoint = new Transform();
		this.maxBullets = 30;
		this.bulletsLeft = maxBullets;
		this.runRotation = new Vec3(10f, -20f, 0);
		this.audioGain = 1.0f;
	}
	
	public void update(GameCore core)
	{
		super.update(core);
		ClientPlayer player = core.getGame().getPlayer();
		if (holder.getID() == player.getID() && !zoomed)
		{
			ECKeyMovement movement = player.getKeyComponent();
			float dx = player.getMouseComponent().getDx() * player.getMouseComponent().getSpeed() * 2.0f;
			float dy = player.getMouseComponent().getDy() * player.getMouseComponent().getSpeed() * 2.0f;
			Vec3 movementVelocity = new Vec3(movement.getVelocity(1)).mul(1, 0, 1);
			if (movement.isRun())
			{
				super.updateBobbing(movementVelocity.magnitude(), 0.2f, 0.3f);
				super.updateRunPosition();
			}
			else
			{
				super.updateBobbing(movementVelocity.magnitude(), 0.15f, 0.2f);
				super.updateWeaponVelocity(movement.getVelocity(1), dx, dy, 0.0008f);
			}
		}

		if (reloading)
		{
			if(reloadTimer > reloadTime)
			{
				reloading = false;
				bulletsLeft = maxBullets;
				shot = false;
			}

			reloadTimer++;
			return;
		}

		if (shooting)
		{
			if (!shot)
			{
				shot = true;
				shootTimer = 0;
				shootBullet(core);
			}
			
			if (shootTimer > 60 / fireFrequency)
			{
				shot = false;
			}
		}
		if (shot)
		{
			shootTimer++;
		}
		shooting = false;
	}
	
	private void shootBullet(GameCore core)
	{
		Bullet bullet = new Bullet(Indexer.getUniqueID(), holder.getID(), "", this.shootPoint.getPosition(), this.transform.getRotation(), shootForce, damage, GameCore.getInstance().getGame().getPlayer().getID());
		net.send(new BulletShootPacket(holder.getID(), bullet), Protocol.UDP);
		net.send(new SoundPacket(holder.getID(), new AudioSource(fireSound, this.shootPoint.getPosition())), Protocol.TCP);
		bullet.setNetwork(net);
		core.getGame().spawn(bullet);

		if (zoomed)
		{
			this.rotationFactor.add(-recoilOnZoom, 0, 0);
		}else{
			this.rotationFactor.add(-recoil, 0, 0);
		}
	}
	
	public void onAction()
	{
		shoot();
	}

	public void onActionUp()
	{
	}

	public void onActionDown()
	{
	}

	public void shoot()
	{
		shooting = true;
		if (bulletsLeft == 0 && !reloading)
		{
			reloadBullets();
			return;
		}

		if (shot)
			return;
		if (zoomed)
			shootPecision = shootPecisionZoomed;
		else
			shootPecision = shootPecisionIdle;


		Vec3 shootVector = new Vec3(transform.getLocalPosition()).sub(transform.getLocalRotation().getForward().copy().mul(0, 0, shootPecision));
		this.transform.setLocalPosition(shootVector);
		this.removeBullet();
		this.holder.getCore().getGame().spawn(new AudioSource(this.fireSound, audioGain));
		if (!zoomed)
		{
			this.rotationFactor.add(Mathf.random(-shootPecisionIdle, shootPecisionIdle), Mathf.random(-shootPecisionIdle * 2, 0), 0);
		}
	}

	private void removeBullet()
	{
		if (bulletsLeft == 0)
			// TODO raise exception
			return;
		bulletsLeft--;
	}

	protected void setShootForce(float shootForce) { this.shootForce = shootForce; }

	public void reloadBullets()
	{
		reloading = true;
		reloadTimer = 0;
	}

	public int getFireFrequency()
	{
		return fireFrequency;
	}

	public void setFireFrequency(int fireFrequency)
	{
		this.fireFrequency = fireFrequency;
	}

	public void setShootPecisionIdle(float shootPecisionIdle) {
		this.shootPecisionIdle = shootPecisionIdle;
	}

	public void setShootPecisionZoomed(float shootPecisionZoomed) {
		this.shootPecisionZoomed = shootPecisionZoomed;
	}

	public Transform getShootPoint()
	{
		return shootPoint;
	}

	public float getShootForce()
	{
		return shootForce;
	}

	public float getAudioGain() { return audioGain; }

	public void setShootPoint(Transform shootPoint)
	{
		this.shootPoint = shootPoint;
		this.shootPoint.setParent(this.transform);
	}

	public int getMaxBullets()
	{
		return maxBullets;
	}

	public void setMaxBullets(int maxBullets)
	{
		this.maxBullets = maxBullets;
	}

	public int getBulletsLeft()
	{
		return bulletsLeft;
	}

	public void setBulletsLeft(int bulletsLeft)
	{
		this.bulletsLeft = bulletsLeft;
	}

	public void setAudioGain(float audioGain) { this.audioGain = audioGain; }

	public void setRecoil(float f) { this.recoil = f; }

	public void setRecoilOnZoom(float f) { this.recoilOnZoom = f; }

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public int getDamage() {
		return damage;
	}

	public void setReloadTime(int reloadTime) {
		this.reloadTime = reloadTime;
	}

	public int getReloadTime() {
		return this.reloadTime;
	}

	// and in seconds
	public void setReloadTimeSeconds(float reloadTime) {
		this.reloadTime = (int)(60 * reloadTime);
	}

	public float getReloadTimeSeconds() {
		return 60f / this.reloadTime;
	}
}
