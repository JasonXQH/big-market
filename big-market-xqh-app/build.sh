# 普通镜像构建，随系统版本构建 amd/arm
#docker build -t jasonxqh/big-market-xqh-app:1.0 -f ./Dockerfile .

#只构建linux/amd64
docker build --platform linux/amd64 -t jasonxqh/big-market-xqh-app:1.0 -f ./Dockerfile .

# 兼容 amd、arm 构建镜像
# docker buildx build --load --platform liunx/amd64,linux/arm64 -t xiaofuge/xfg-frame-archetype-app:1.0 -f ./Dockerfile . --push
# docker buildx build --load --platform linux/amd64,linux/arm64 -t jasonxqh/big-market-xqh-app:1.0 -f ./Dockerfile . --push
