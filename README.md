# ClinicRecord

ClinicRecord 是一个用于诊所日常患者和就诊记录管理的 Android 应用。项目使用 Kotlin 与 Jetpack Compose 开发，支持患者信息管理、就诊记录录入、详情查看、回收站、个人资料和设置等基础功能。

## 功能特点

- 患者列表管理
- 新增、编辑和查看患者信息
- 创建和查看就诊记录
- 软删除与回收站管理
- 本地数据库存储
- 基于 Jetpack Compose 的现代 Android 界面

## 技术栈

- Kotlin
- Android Jetpack Compose
- Room Database
- Gradle Kotlin DSL
- Material Design 3

## 项目结构

```text
app/src/main/java/com/example/clinicrecord
├── data          # 数据库、实体、DAO、迁移和接口
├── navigation    # 页面导航
├── ui            # 应用界面
├── ui/theme      # 主题样式
└── viewmodel     # 页面状态与业务逻辑
