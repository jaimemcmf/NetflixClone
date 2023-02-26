ffmpeg -re -i "$1" -vcodec libx264 -vprofile baseline -g 30 -acodec aac -strict -2 -f flv rtmp://{ip}/show/$2;
