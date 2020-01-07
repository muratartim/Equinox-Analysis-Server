/*
 * Copyright 2018 Murat Artim (muratartim@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package equinox.analysisServer.remote;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

import equinox.analysisServer.remote.data.AnalysisServerStatistic;
import equinox.analysisServer.remote.data.IsamiMaterial;
import equinox.analysisServer.remote.listener.AnalysisMessageListener;
import equinox.analysisServer.remote.message.AnalysisComplete;
import equinox.analysisServer.remote.message.AnalysisFailed;
import equinox.analysisServer.remote.message.AnalysisMessage;
import equinox.analysisServer.remote.message.AnalysisProgress;
import equinox.analysisServer.remote.message.AnalysisRequest;
import equinox.analysisServer.remote.message.AnalysisServerStatisticsRequest;
import equinox.analysisServer.remote.message.AnalysisServerStatisticsRequestFailed;
import equinox.analysisServer.remote.message.AnalysisServerStatisticsResponse;
import equinox.analysisServer.remote.message.DAAIncrementComplete;
import equinox.analysisServer.remote.message.DCAIncrementComplete;
import equinox.analysisServer.remote.message.ESAComplete;
import equinox.analysisServer.remote.message.FastESAComplete;
import equinox.analysisServer.remote.message.FlightDCAComplete;
import equinox.analysisServer.remote.message.FullESAComplete;
import equinox.analysisServer.remote.message.HandshakeWithAnalysisServer;
import equinox.analysisServer.remote.message.IsamiESARequest;
import equinox.analysisServer.remote.message.RestartAnalysisServerRequest;
import equinox.analysisServer.remote.message.RestartAnalysisServerRequestFailed;
import equinox.analysisServer.remote.message.RestartAnalysisServerResponse;
import equinox.analysisServer.remote.message.SafeDAAIncrementRequest;
import equinox.analysisServer.remote.message.SafeDCAIncrementRequest;
import equinox.analysisServer.remote.message.SafeESARequest;
import equinox.analysisServer.remote.message.SafeFlightDCARequest;
import equinox.analysisServer.remote.message.StopAnalysisServerRequest;
import equinox.analysisServer.remote.message.StopAnalysisServerRequestFailed;
import equinox.analysisServer.remote.message.StopAnalysisServerResponse;
import equinox.serverUtilities.BigMessage;
import equinox.serverUtilities.FilerConnection;
import equinox.serverUtilities.NetworkMessage;
import equinox.serverUtilities.PartialMessage;
import equinox.serverUtilities.Permission;
import equinox.serverUtilities.ServerUtility;
import equinox.serverUtilities.SplitMessage;

/**
 * Class for object registry. This class registers objects that are going to be sent over the network.
 *
 * @version 1.0
 * @author Murat Artim
 * @time 4:01:25 PM
 * @date Jul 10, 2011
 */
public class Registry {

	/**
	 * This registers objects that are going to be sent over the network.
	 *
	 * @param endPoint
	 *            End point to retrieve the kryo object serializer.
	 */
	public static void register(EndPoint endPoint) {

		// get object serializer
		Kryo kryo = endPoint.getKryo();

		// register JDK classes
		kryo.register(String[].class);
		kryo.register(char[].class);
		kryo.register(byte[].class);
		kryo.register(int[].class);
		kryo.register(int[][].class);
		kryo.register(long[].class);
		kryo.register(boolean[].class);
		kryo.register(List.class);
		kryo.register(ArrayList.class);
		kryo.register(Vector.class);
		kryo.register(HashMap.class);
		kryo.register(Exception.class);
		kryo.register(StackTraceElement.class);
		kryo.register(Timestamp.class);
		kryo.register(Date.class);

		// register utility classes
		kryo.register(NetworkMessage.class);
		kryo.register(BigMessage.class);
		kryo.register(PartialMessage.class);
		kryo.register(SplitMessage.class);
		kryo.register(FilerConnection.class);
		kryo.register(ServerUtility.class);
		kryo.register(Permission.class);
		kryo.register(Permission[].class);

		// register data classes
		kryo.register(IsamiMaterial.class);
		kryo.register(AnalysisServerStatistic.class);
		kryo.register(AnalysisServerStatistic[].class);

		// register message classes
		kryo.register(HandshakeWithAnalysisServer.class);
		kryo.register(AnalysisMessage.class);
		kryo.register(AnalysisRequest.class);
		kryo.register(IsamiESARequest.class);
		kryo.register(SafeDAAIncrementRequest.class);
		kryo.register(SafeDCAIncrementRequest.class);
		kryo.register(SafeESARequest.class);
		kryo.register(SafeFlightDCARequest.class);
		kryo.register(AnalysisFailed.class);
		kryo.register(AnalysisProgress.class);
		kryo.register(AnalysisComplete.class);
		kryo.register(DAAIncrementComplete.class);
		kryo.register(DCAIncrementComplete.class);
		kryo.register(ESAComplete.class);
		kryo.register(FastESAComplete.class);
		kryo.register(FullESAComplete.class);
		kryo.register(FlightDCAComplete.class);
		kryo.register(AnalysisServerStatisticsRequest.class);
		kryo.register(AnalysisServerStatisticsResponse.class);
		kryo.register(AnalysisServerStatisticsRequestFailed.class);
		kryo.register(StopAnalysisServerRequest.class);
		kryo.register(StopAnalysisServerResponse.class);
		kryo.register(StopAnalysisServerRequestFailed.class);
		kryo.register(RestartAnalysisServerRequest.class);
		kryo.register(RestartAnalysisServerResponse.class);
		kryo.register(RestartAnalysisServerRequestFailed.class);

		// register listener classes
		kryo.register(AnalysisMessageListener.class);
	}
}