package com.mt.mtuser.service

import com.mt.mtuser.dao.department.DepartmentDao
import com.mt.mtuser.dao.department.DepartmentPostDao
import com.mt.mtuser.dao.department.PostDao
import com.mt.mtuser.entity.department.Department
import com.mt.mtuser.entity.department.DepartmentPost
import com.mt.mtuser.entity.department.DepartmentPostInfo
import com.mt.mtuser.entity.department.Post
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.convert.EntityRowMapper
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.awaitOneOrNull
import org.springframework.r2dbc.core.flow
import org.springframework.stereotype.Service
import java.util.*

/**
 * Created by gyh on 2020/7/20
 */
@Service
class DepartmentPostService {
    @Autowired
    lateinit var departmentDao: DepartmentDao

    @Autowired
    lateinit var postDao: PostDao

    @Autowired
    lateinit var departmentPostDao: DepartmentPostDao

    @Autowired
    protected lateinit var template: R2dbcEntityTemplate

    @Autowired
    lateinit var r2dbcService: R2dbcService

    @Autowired
    lateinit var roleService: RoleService
    private val bindMutex = Mutex()


    suspend fun bindDepartment(dpi: DepartmentPostInfo): DepartmentPost {
        bindMutex.withLock {
            val department = departmentDao.findByName(dpi.departmentName!!)
                ?: departmentDao.save(Department(name = dpi.departmentName))
            val post = dpi.postName?.let { postDao.findByName(it) ?: postDao.save(Post(name = it)) }
            return existRelated(department.id!!, post?.id, dpi.companyId!!)
                ?: departmentPostDao.save(
                    DepartmentPost(
                        departmentId = department.id,
                        postId = post?.id,
                        companyId = dpi.companyId
                    )
                )
        }
    }

    suspend fun updateBind(dpi: DepartmentPostInfo): Int {
        bindMutex.withLock {
            val dp = findByDpId(dpi.id!!) ?: error("不存在该id：${dpi.id}")
            if (dp == dpi) error("请至少修改一个属性")
            val departmentId = dpi.departmentName?.let {
                departmentDao.findByName(it) ?: departmentDao.save(Department(name = it))
            }?.id
            val postId = dpi.postName?.let {
                postDao.findByName(it) ?: postDao.save(Post(name = it))
            }?.id
            val departmentPost = DepartmentPost(id = dpi.id!!, departmentId = departmentId, postId = postId)
            return r2dbcService.dynamicUpdate(departmentPost)
                .awaitSingle()
        }
    }

    suspend fun deleteBind(id: Int) {
        if (roleService.existsByDpId(id) > 0) {
            error("该部门下存在用户")
        } else {
            departmentPostDao.deleteById(id)
        }
    }

    suspend fun findByDpId(id: Int): DepartmentPostInfo? {
        return template.databaseClient.sql(
            "select mdp.id, md.name as department_name, mp.name as post_name " +
                    " from mt_department_post mdp " +
                    " left join mt_department md on mdp.department_id = md.id " +
                    " left join mt_post mp on mdp.post_id = mp.id " +
                    " where mdp.id = :id"
        )
            .bind("id", id)
            .map(EntityRowMapper(DepartmentPostInfo::class.java, template.converter))
            .awaitOneOrNull()
    }

    suspend fun findAllBind(companyId: Int): LinkedList<Department> {
        val dataList = template.databaseClient.sql(
            "select mdp.id, md.name as dname, mp.name as pname " +
                    " from mt_department_post mdp " +
                    " left join mt_department md on mdp.department_id = md.id " +
                    " left join mt_post mp on mdp.post_id = mp.id " +
                    " where company_id = :companyId "
        )
            .bind("companyId", companyId)
            .map { r, _ ->
                val dpi = DepartmentPostInfo()
                dpi.id = r.get("id", java.lang.Integer::class.java)?.toInt()
                dpi.departmentName = r.get("dname", String::class.java)
                dpi.postName = r.get("pname", String::class.java)
                dpi
            }.flow()
        val list = LinkedList<Department>()
        dataList.collect {
            val department = list.find { d -> d.name == it.departmentName }
                ?: Department(name = it.departmentName)
            val post = Post(id = it.id, name = it.postName)
            department.postList.add(post)
            if (!list.contains(department)) {
                list.add(department)
            }
        }
        return list
    }

    suspend fun existRelated(departmentId: Int, postId: Int?, companyId: Int): DepartmentPost? {
        val subSql = if (postId == null) "post_id is null" else "post_id = $postId"
        return template.databaseClient.sql("select * from mt_department_post where company_id = :companyId and department_id = :departmentId and $subSql limit 1")
            .bind("companyId", companyId)
            .bind("departmentId", departmentId)
            .map(EntityRowMapper(DepartmentPost::class.java, template.converter))
            .awaitOneOrNull()
    }

}