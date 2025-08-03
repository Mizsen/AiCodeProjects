# 创建并切换到一个新的孤立分支
git checkout --orphan new-main

# 添加所有文件到暂存区
git add -A

# 提交这些文件作为新的初始提交
git commit -m "Initial commit"

# 删除原来的主分支（假设原主分支是 main）
git branch -D main

# 将当前分支重命名为 main
git branch -m main

# 强制推送到远程仓库，覆盖远程历史
# 注意：这会彻底删除远程仓库的所有历史，操作前请确保无误
git push -f origin main