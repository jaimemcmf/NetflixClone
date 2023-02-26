ffmpeg -i "$1" -vf "select=eq(n\,100)" -q:v 3 "$2"
