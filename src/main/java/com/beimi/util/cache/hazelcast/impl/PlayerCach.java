package com.beimi.util.cache.hazelcast.impl;

import java.util.*;

import com.beimi.web.model.PlayUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.beimi.core.BMDataContext;
import com.beimi.util.cache.PlayerCacheBean;
import com.beimi.web.model.GameRoom;
import com.beimi.web.model.PlayUserClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.query.PagingPredicate;
import com.hazelcast.query.SqlPredicate;

@Service("multi_cache")
public class PlayerCach implements PlayerCacheBean{

    //@Autowired
    //public HazelcastInstance hazelcastInstance;

    public static Map<String,Object> hazelcastInstance = new HashMap<String,Object>();


    public Map<String,Object> getInstance(){
        return hazelcastInstance ;
    }

    public void clean(String roomid ,String orgi) {
        List<PlayUserClient> palyers = getCacheObject(roomid , orgi );
        if(palyers!=null && palyers.size() > 0){
            for(PlayUserClient player : palyers){
                this.delete(player.getId() , orgi) ;
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<PlayUserClient> getCacheObject(String key, String orgi) {
		/*PagingPredicate<String, GameRoom> pagingPredicate = null ;
		List playerList = new ArrayList();
		pagingPredicate = new PagingPredicate<String, GameRoom>(  new SqlPredicate( " roomid = '" + key + "'") , 100 );
		playerList.addAll((getInstance().getMap(getName())).values(pagingPredicate) ) ;*/
        return (List<PlayUserClient>) getInstance().get(key);
        //return playerList;
    }

    public String getName() {
        return BMDataContext.BEIMI_GAME_PLAYWAY ;
    }
    @Override
    public void put(String key, Object value, String orgi) {
        if(getInstance().containsKey(key)){
            for(PlayUserClient playUserClient : ((List<PlayUserClient>)getInstance().get(key))){
                if(playUserClient.getId().equals(((PlayUserClient)value).getId())){
                    return;
                }
            }
            ((List)getInstance().get(key)).add(value);
            System.out.println(((List)getInstance().get(key)).size());
            List<Object> obj = ((List)getInstance().get(key));
        }else{
            List<PlayUserClient> playUserClients = new ArrayList<PlayUserClient>();
            playUserClients.add((PlayUserClient) value);
            getInstance().put(key, playUserClients) ;
        }
    }
    @Override
    public Object delete(String key, String orgi) {
        return getInstance().remove(key) ;
    }
    @Override
    public Object getPlayer(String key, String orgi) {
        //TODO 便利全部USER太耗新能 后期优化
        if(getInstance().get(key) == null || getInstance().get(key) == null){
            return null;
        }

        Collection<Object> collections = getInstance().values();
        for(Iterator<Object> it = collections.iterator(); it.hasNext();){

            for(PlayUserClient playUserClient : (List<PlayUserClient>)it.next()){

                if(playUserClient.getId().equals(key)){
                    return playUserClient;
                }
            }
        }

        return null;
    }

    @Override
    public Object getCache() {
        return getInstance().get(getName());
    }
    @Override
    public long getSize() {
        // TODO Auto-generated method stub
        return getInstance().size();
    }

    @Override
    public PlayerCacheBean getCacheInstance(String string) {
        return this;
    }
}
