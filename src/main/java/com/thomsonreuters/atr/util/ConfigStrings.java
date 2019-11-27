package com.thomsonreuters.atr.util;

public class ConfigStrings {

    //.yml config file path
    public static String ConfigFilesPath =  "/app/RESTelasticsearch";


    //ELASTIC
    public static final String HOST = "/ElasticService/ElasticDataSource/Host";
    public static final String PORT = "/ElasticService/ElasticDataSource/Port";
    public static final String Protocol =  "/ElasticService/ElasticDataSource/Protocol";
    public static final String RequestTimeout =  "/ElasticService/ElasticDataSource/RequestTimeout";
    public static final String MessageIndexName =  "/ElasticService/ElasticDataSource/MessageIndex/IndexName";
    public static final String LogIndexName = "/ElasticService/ElasticDataSource/LogIndex/IndexName";
    public static final String MessageMaxResultSize =  "/ElasticService/ElasticDataSource/MessageIndex/MaxResultSize";



    //POSTGRES
    public static final String PostgresURL = "/PostgresDB/URL";
    public static final String PostgresUser = "/PostgresDB/User";
    public static final String PostgresPassword = "/PostgresDB/Password";
    public static final String PostgresPlatform = "/PostgresDB/Platform";

}
