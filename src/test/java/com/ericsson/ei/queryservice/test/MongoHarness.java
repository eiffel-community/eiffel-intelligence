/***********************************************************************
 *                                                                     *
 * Copyright Ericsson AB 2017                                          *
 *                                                                     *
 * No part of this software may be reproduced in any form without the  *
 * written permission of the copyright owner.                          *
 *                                                                     *
 ***********************************************************************/
package com.ericsson.ei.queryservice.test;

import com.mongodb.client.MongoCollection;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.bson.Document;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;

public class MongoHarness {

	static String host = "localhost";
	static int port = 27018;

	static MongoCollection<Document> col;

	private static MongodExecutable mongodExecutable;

	@BeforeClass
	public static void beforeClass() throws IOException {
		final MongodStarter starter = MongodStarter.getDefaultInstance();
		final IMongodConfig mongodConfig = new MongodConfigBuilder().version(Version.Main.PRODUCTION)
				.net(new Net(host, port, Network.localhostIsIPv6())).build();
		mongodExecutable = starter.prepare(mongodConfig);
		mongodExecutable.start();
	}

	@AfterClass
	public static void afterClass() {
		if (mongodExecutable != null) {
			mongodExecutable.stop();
		}
	}

}
