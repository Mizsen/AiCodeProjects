#!/bin/bash
set -e

# 定义文件路径
BASE_DIR="qiye-project/202507210931/python-crawler/chrome114-win64"
CHROME_ZIP="$BASE_DIR/chrome-win64.zip"
CHROMEDRIVER_ZIP="$BASE_DIR/chromedriver_win32.zip"

# 改进的Git LFS检查函数
check_git_lfs() {
    # 尝试多种方式检查Git LFS
    if command -v git-lfs &> /dev/null; then
        return 0
    elif git lfs version &> /dev/null; then
        return 0
    else
        return 1
    fi
}

# 检查是否有未提交的更改
check_uncommitted_changes() {
    if [[ -n $(git status --porcelain) ]]; then
        return 0  # 有未提交的更改
    else
        return 1  # 没有未提交的更改
    fi
}

# 检查Git LFS
if ! check_git_lfs; then
    echo "错误: 未检测到Git LFS，请先安装"
    echo "安装命令参考:"
    echo "  macOS: brew install git-lfs"
    echo "  Linux: sudo apt install git-lfs (Debian/Ubuntu) 或 sudo dnf install git-lfs (Fedora)"
    echo "  Windows: 从 https://git-lfs.com/ 下载安装"
    echo "安装后请确保执行: git lfs install"
    exit 1
fi

# 验证Git LFS是否正确初始化
if ! git lfs env &> /dev/null; then
    echo "正在初始化Git LFS..."
    git lfs install || {
        echo "错误: Git LFS初始化失败"
        exit 1
    }
fi

# 检查目录和文件是否存在
if [ ! -d "$BASE_DIR" ]; then
    echo "错误: 目录 $BASE_DIR 不存在"
    exit 1
fi

if [ ! -f "$CHROME_ZIP" ]; then
    echo "错误: 未找到文件 $CHROME_ZIP"
    exit 1
fi

if [ ! -f "$CHROMEDRIVER_ZIP" ]; then
    echo "错误: 未找到文件 $CHROMEDRIVER_ZIP"
    exit 1
fi

# 检查Git仓库
if [ ! -d ".git" ]; then
    echo "错误: 当前目录不是Git仓库，请先初始化仓库"
    exit 1
fi

# 处理未提交的更改
if check_uncommitted_changes; then
    echo "检测到未提交的更改，尝试暂存这些更改..."
    # 暂存所有更改
    git stash push -u -m "自动暂存: $(date +'%Y-%m-%d %H:%M:%S')" || {
        echo "错误: 暂存未提交的更改失败，请手动提交或暂存更改后再试"
        exit 1
    }
    STASHED=1
else
    STASHED=0
fi

# 拉取远程最新更改，使用--rebase策略
echo "正在拉取远程最新更改..."
if ! git pull --rebase origin main; then
    echo "拉取时发生冲突，尝试取消rebase并使用合并策略..."
    git rebase --abort || true  # 取消可能的rebase，即使没有rebase也不报错
    
    if ! git pull --no-rebase origin main; then
        echo "错误: 拉取远程更改失败，存在合并冲突"
        echo "请手动解决冲突："
        echo "1. 运行 git pull origin main"
        echo "2. 解决冲突文件中的冲突标记"
        echo "3. 运行 git add . 标记已解决的冲突"
        echo "4. 运行 git commit -m '解决合并冲突'"
        echo "5. 重新运行本脚本"
        
        # 如果之前暂存了更改，恢复它们
        if [ $STASHED -eq 1 ]; then
            echo "恢复之前暂存的更改..."
            git stash pop || true
        fi
        exit 1
    fi
fi

# 如果之前暂存了更改，恢复它们
if [ $STASHED -eq 1 ]; then
    echo "恢复之前暂存的更改..."
    git stash pop || {
        echo "警告: 恢复暂存的更改时发生冲突，请手动解决"
    }
fi

# 跟踪指定路径的大文件
git lfs track "$CHROME_ZIP"
git lfs track "$CHROMEDRIVER_ZIP"

# 添加文件到暂存区
git add .gitattributes
git add "$CHROME_ZIP"
git add "$CHROMEDRIVER_ZIP"

# 检查是否有需要提交的内容
if ! git diff --cached --quiet; then
    # 提交文件（修复日期格式）
    commit_msg="Add Chrome and Chromedriver binaries: $(date +'%Y-%m-%d %H:%M:%S')"
    git commit -m "$commit_msg"
else
    echo "没有新的更改需要提交"
fi

# 推送到远程仓库
echo "正在推送文件到远程仓库..."
git push origin main

echo "操作完成！文件已上传至Git仓库"
