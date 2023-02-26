# Netflix like Android Application

> ## __This project consists of:__
>
> - User application used to watch streams
> - CMS application used to upload and delete movies and edit user settings
> - Application Server responsible for storing, streaming and processing the movies/streams
>
> ### __Motivation__
> - This project was developed as an assignment of a Mobile Programing class.
> The proposed idea was to develop an Android Application similar to Netflix but that allowed users to upload their own movies and be able to watch those movies in the form of a stream using NGINX. The application is divided as mentioned above into 3 parts, all developed by the group.
>
> ### __About the server__
> - The server uses Jetty and Jersey as it's foundation, developed with the 
> maven building tool. The server was designed to be executed in a google cloud 
> even though it can be easily adapted to other environments.
> It's responsible for storing the movies (The movies are stored locally 
> in the server and in a google bucket, in case NGINX is down for any reason), 
> generating a thumbnail for the movie in the case one is not provided, stream 
> the movies using NGINX and generate a 360p version of the movie for users
> with a weak network connection.
> In order to store all the information, MariaDB was used being the connection
> between the server and the DB possible using JDBC and Prepared Statements.
>> NOTE: The movies weren't saved in the database it self but rather it's 
>> path to the folder where they were stored.


