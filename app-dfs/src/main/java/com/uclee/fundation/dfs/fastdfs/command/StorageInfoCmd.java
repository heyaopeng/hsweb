package com.uclee.fundation.dfs.fastdfs.command;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.uclee.fundation.dfs.fastdfs.data.Result;
import com.uclee.fundation.dfs.fastdfs.data.StorageInfo;

public class StorageInfoCmd extends AbstractCmd<List<StorageInfo>> {

	@Override
	public Result<List<StorageInfo>> exec(Socket socket) throws IOException {
		request(socket.getOutputStream());
		Response response = response(socket.getInputStream());
		if(response.isSuccess()){
			byte[] data = response.getData();
			int dataLength = data.length;
			if(dataLength%StorageInfo.BYTE_SIZE!=0){
				throw new IOException("recv body length: " + data.length + " is not correct");
			}
			List<StorageInfo> storageInfos = new ArrayList<StorageInfo>();
			int offset = 0;
			while(offset<dataLength){
				StorageInfo storageInfo = new StorageInfo(data,offset);
				storageInfos.add(storageInfo);
				offset += StorageInfo.BYTE_SIZE;
			}
			return new Result<List<StorageInfo>>(response.getCode(), storageInfos);
		}else{
			return new Result<List<StorageInfo>>(response.getCode(), "Error");
		}
	}

	public StorageInfoCmd(String group) throws UnsupportedEncodingException {
		super();
		int group_len;
		byte[] bs = group.getBytes("UTF-8");
		body1 = new byte[FDFS_GROUP_NAME_MAX_LEN];
		if (bs.length <= FDFS_GROUP_NAME_MAX_LEN) {
			group_len = bs.length;
		} else {
			group_len = FDFS_GROUP_NAME_MAX_LEN;
		}
		Arrays.fill(body1, (byte) 0);
		System.arraycopy(bs, 0, body1, 0, group_len);
		this.requestCmd = TRACKER_PROTO_CMD_SERVER_LIST_STORAGE;
		this.responseCmd = TRACKER_PROTO_CMD_RESP;
		this.responseSize = -1;
	}

	public StorageInfoCmd(String group,String ip) throws UnsupportedEncodingException {
		super();
		int group_len;
		byte[] groupByte = group.getBytes("UTF-8");
		byte[] ipByte = ip.getBytes("UTF-8");
		body1 = new byte[FDFS_GROUP_NAME_MAX_LEN + ipByte.length];
		if (groupByte.length <= FDFS_GROUP_NAME_MAX_LEN) {
			group_len = groupByte.length;
		} else {
			group_len = FDFS_GROUP_NAME_MAX_LEN;
		}
		Arrays.fill(body1, (byte) 0);
		System.arraycopy(groupByte, 0, body1, 0, group_len);
		System.arraycopy(ipByte, 0, body1, FDFS_GROUP_NAME_MAX_LEN, ipByte.length);
		this.requestCmd = TRACKER_PROTO_CMD_SERVER_LIST_STORAGE;
		this.responseCmd = TRACKER_PROTO_CMD_RESP;
		this.responseSize = -1;
	}
	
	

}
